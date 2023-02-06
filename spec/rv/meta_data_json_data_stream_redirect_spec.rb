require 'spec_helper'

describe 'redirects for the research video project' do

  let :media_entry do
    FactoryBot.create(:research_video_media_entry,
                       get_metadata_and_previews: true,
                       get_full_size: true)
  end

  let :http_client do
    Faraday.new(url: api_base_url) do |conn|
      # yield conn if block_given? # FIXME: this fails with `Invalid yield (SyntaxError)` since ruby 2.7 BUT also seems obsolete???
      conn.adapter Faraday.default_adapter
    end
  end

  context 'the response of the "media-entries/:media_entry_id/meta-data/:meta_key_id/data-stream"' do

    let :response do
      http_client.get "media-entries/#{media_entry.id}/meta-data/" \
        "video_research_project:annotation/data-stream"
    end

    let :redirect_url do
      response.headers['location']
    end

    it 'redirects to the media-file data-stream' do
      expect(response.status).to be== 302
      expect(redirect_url).to match /\/api\/meta-data\/[^\/]+\/data-stream/
    end


    context "the response of the redirect" do

      let :response_of_redirect do
        http_client.get redirect_url do |conn|
          conn.headers['Accept'] = 'application/json'
        end
      end

      it 'returns the original file' do
        expect(response_of_redirect.status).to be== 200
        expect(response_of_redirect.headers['content-type']).to start_with 'application/json'
        expect(JSON.parse(response_of_redirect.body)["general"]["title"]).to be==
          "(Sample Project) The Dot and the Line"
      end

    end

  end

end
