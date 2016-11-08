require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'Getting a media-file resource with authentication' do

  before :each do
    @media_entry = FactoryGirl.create(:media_entry_with_image_media_file,
                                      get_metadata_and_previews: false,
                                      get_full_size: false)
    @media_file = @media_entry.media_file
    @entity = FactoryGirl.create(:api_client, password: 'password')
  end

  context 'the resource itself' do

    include_context :auth_media_file_resource_via_json_roa

    context :check_allowed_if_api_client_permission do
      before :each do
        @media_entry.api_client_permissions << \
          FactoryGirl.create(:media_entry_api_client_permission,
                             get_metadata_and_previews: true,
                             api_client: @entity)
      end

      it 'is allowed 200' do
        expect(response.status).to be == 200
      end
    end

  end

  context 'the data stream' do
    context 'with get_full_size permission' do
      include_context :auth_media_file_original_data_stream_via_json_roa
      before :each do
        @media_entry.api_client_permissions << \
          FactoryGirl.create(:media_entry_api_client_permission,
                             get_metadata_and_previews: true,
                             get_full_size: true,
                             api_client: @entity)
      end
      it do
        expect(response.status).to be == 200
      end
    end


    context 'without get_full_size permission' do
      include_context :auth_media_file_original_data_stream_via_json_roa
      before :each do
        @media_entry.api_client_permissions << \
          FactoryGirl.create(:media_entry_api_client_permission,
                             get_metadata_and_previews: true,
                             get_full_size: false,
                             api_client: @entity)
      end
      it do
        expect(response.status).to be == 403
      end
    end

  end

end

