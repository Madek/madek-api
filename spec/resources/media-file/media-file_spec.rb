require 'spec_helper'

describe 'MediaFile Resource' do
  include_context :authenticated_json_roa_client

  describe 'requesting a non existing media-resource' do
    let :resource do
      authenticated_json_roa_client.get.relation('media-file') \
        .get('id' => SecureRandom.uuid)
    end
    let :response do
      resource.response
    end

    it 'responds with 404 not found' do
      expect(response.status).to be == 404
    end
  end

  describe 'the image media file' do
    let :media_entry do
      media_entry = FactoryBot.create :media_entry,
                                       get_full_size: true,
                                       get_metadata_and_previews: true
    end

    let :media_file do
      FactoryBot.create :media_file_for_image, media_entry: media_entry
    end

    it do
      expect(media_file).to be
    end

    describe 'the media-entry resource' do
      let :media_entry_resource do
        authenticated_json_roa_client.get.relation('media-entry')
          .get('id' => media_file.media_entry.id)
      end
      it 'has a media-file relation' do
        expect(media_entry_resource.relation('media-file')).to be_a JSON_ROA::Client::Relation
      end
      it 'links to the media-file resource' do
        expect(media_entry_resource.relation('media-file').data['href']).to \
          match %r{.*/media-files/#{media_file.id}}
      end
    end

    describe 'the media-file resource' do
      let :resource do
        authenticated_json_roa_client.get.relation('media-file') \
          .get('id' => media_file.id)
      end

      describe 'the response' do
        let :response do
          resource.response
        end

        describe 'the status ' do
          it 'is 200 OK' do
            expect(response.status).to be == 200
          end
        end

        describe 'the response body ' do

          it 'includes the content_type key' do
            expect(response.body.keys).to include "content_type"
          end

          it 'includes the media_type key' do
            expect(response.body.keys).to include "media_type"
          end

        end
      end

      describe 'the data-stream resource' do
        let :data_stream_resource do
          resource.relation('data-stream').get
        end

        describe 'the response' do
          let :data_stream_resource_response do
            data_stream_resource.response
          end

          it do
            expect(data_stream_resource_response.status).to be == 200
          end

          context 'for not existing file' do
            let :media_file do
              FactoryBot.create :media_file, media_entry: media_entry
            end

            it 'responds with 404' do
              expect(data_stream_resource_response.status).to be == 404
            end
          end

          describe 'the content-type header' do
            let :content_type do
              data_stream_resource_response.headers['content-type']
            end
            it do
              expect(content_type).to be == 'image/jpeg'
            end
          end

          describe 'the body' do
            let :body do
              data_stream_resource_response.body
            end
            it 'has the proper hashsum' do
              expect(Digest::SHA256.hexdigest body).to be ==
                '66e1eb76ef8079968ff6a3e7519749be3fbb7b05d54a6f1270727273ccb2a539'
            end
          end
        end
      end
    end
  end
end
