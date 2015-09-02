require 'spec_helper'
require "#{Rails.root}/spec/resources/media-entry/shared.rb"

describe 'Getting a media-entry resource with authentication' do
  before :example do
    @media_entry = FactoryGirl.create(:media_entry,
                                      get_metadata_and_previews: false)
    @entity = FactoryGirl.create(:api_client, password: 'password')
  end

  include_context :auth_media_entry_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    it 'is forbidden 403' do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_api_client_permission do
    before :example do
      @media_entry.api_client_permissions << \
        FactoryGirl.create(:media_entry_api_client_permission,
                           api_client: @entity)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end
end
