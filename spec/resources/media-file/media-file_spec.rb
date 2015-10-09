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
    let :media_file do
      media_entry = FactoryGirl.create :media_entry,
                                      get_full_size: true,
                                      responsible_user: user
      FactoryGirl.create :media_file_for_image,
                         media_entry: media_entry
    end

    it do
      expect(media_file).to be
    end

    describe 'the media-file resource' do
      let :resource do
        authenticated_json_roa_client.get.relation('media-file') \
          .get('id' => media_file.id)
      end

      describe 'the response status ' do
        let :response do
          resource.response
        end

        it 'is 200 OK' do
          expect(response.status).to be == 200
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
