require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'Getting a preview resource with authentication' do
  before :example do
    @media_entry = FactoryGirl.create(:media_entry_with_image_media_file,
                                      get_metadata_and_previews: false)
    @preview = @media_entry.media_file.previews.sample
    @entity = FactoryGirl.create(:api_client, password: 'password')
  end

  include_context :auth_preview_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    before :example do
      @media_entry.api_client_permissions << \
        FactoryGirl.create(:media_entry_api_client_permission,
                           get_metadata_and_previews: false,
                           api_client: @entity)
    end
    it 'is forbidden 403' do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_api_client_permission do
    before :example do
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
