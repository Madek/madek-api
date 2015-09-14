require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'Getting a media-entry resource without authentication' do
  before :example do
    @media_entry = FactoryGirl.create(:media_entry,
                                      get_metadata_and_previews: false)
  end

  shared_context :check_not_authenticated_without_public_permission do
    it 'is forbidden 401' do
      expect(response.status).to be == 401
    end
  end

  include_context :check_media_entry_resource_via_any,
                  :check_not_authenticated_without_public_permission
end

describe 'Getting a media-entry resource with authentication' do
  before :example do
    @media_entry = FactoryGirl.create(
      :media_entry, get_metadata_and_previews: false,
                    responsible_user: FactoryGirl.create(:user))
    @entity = FactoryGirl.create(:user, password: 'password')
  end

  include_context :auth_media_entry_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    it 'is forbidden 403' do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_responsible do
    before :example do
      @media_entry.update_attributes! responsible_user: @entity
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_permission do
    before :example do
      @media_entry.user_permissions << \
        FactoryGirl.create(:media_entry_user_permission, user: @entity)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_group_permission do
    before :example do
      group = FactoryGirl.create(:group)
      @entity.groups << group
      @media_entry.group_permissions << \
        FactoryGirl.create(:media_entry_group_permission, group: group)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end
end
