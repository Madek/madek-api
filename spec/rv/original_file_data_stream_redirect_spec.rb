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

  context 'the response of the media-entries/:id/media-file/data-stream path' do

    let :response do
      http_client.get "media-entries/#{media_entry.id}/media-file/data-stream"
    end

    let :redirect_url do
      response.headers['location']
    end

    it 'redirects to the media-file data-stream' do
      expect(response.status).to be== 302

      expect(redirect_url).to be==
        "/api/media-files/#{media_entry.media_file.id}/data-stream"
    end


    context "the response of the redirect" do

      let :response_of_redirect do
        http_client.get redirect_url
      end

      it 'returns the original file' do
        expect(response_of_redirect.headers['content-type']).to be== 'video/mp4'
        expect(response_of_redirect.headers['content-length']).to be== '11061375'
      end

    end

  end

end
