require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

ROUNDS = begin
           Integer(ENV['ROUNDS'].presence)
         rescue
           10
         end

SHAS = \
  { small: '04d0162eee38b38e61ef406dfc2b897073ec4eb7f08772bec00b53ab17a4c824',
    small_125: 'b1c9ca6d87a005d07508bceb66ed056b499da6b39b79706c88a0addc38676291',
    medium: 'c3f6a3eae014d0fd1b09feb6031d21d5c342b6671d816009f30724dd666c8f65',
    large: '4adbe33f15234a0f8d669a2ee0a221a5a0b45bc9e3f9db55fd70984fa8f076a1',
    x_large: '4adbe33f15234a0f8d669a2ee0a221a5a0b45bc9e3f9db55fd70984fa8f076a1',
    maximum: '4adbe33f15234a0f8d669a2ee0a221a5a0b45bc9e3f9db55fd70984fa8f076a1' }

describe 'Getting a random preview for a specific media-entry' do
  before :each do
    media_entry = FactoryGirl.create(:media_entry_with_image_media_file,
                                      get_metadata_and_previews: true)
    previews = media_entry.media_file.previews
    @preview = previews.sample
  end

  (1..ROUNDS).each do |round|
    include_context :preview_resource_via_json_roa

    context "ROUND #{round}" do
      it "is successful (200)" do
        expect(response.status).to be == 200
      end

      describe 'getting the data-stream resource' do
        let :data_stream_resource do
          resource.relation('data-stream').get
        end
        let :data_stream_resource_response do
          data_stream_resource.response
        end
        let :content_type do
          data_stream_resource_response.headers['content-type']
        end
        let :body do
          data_stream_resource_response.body
        end

        it 'is successful (200)' do
          expect(data_stream_resource_response.status).to be == 200
        end

        context 'for not existing file' do
          it 'responds with 404' do
            media_entry = FactoryGirl.create(:media_entry,
                                              get_metadata_and_previews: true)
            media_file = FactoryGirl.create(:media_file,
                                            media_entry: media_entry)
            @preview = FactoryGirl.create(:preview,
                                          media_file: media_file)
            expect(data_stream_resource_response.status).to be == 404
          end
        end

        it 'has the proper content type' do
          expect(content_type).to be == 'image/jpeg'
        end

        it 'has the proper hashsum' do
          expect(Digest::SHA256.hexdigest body)
            .to be == SHAS[@preview.thumbnail.to_sym]
        end
      end
    end
  end
end
