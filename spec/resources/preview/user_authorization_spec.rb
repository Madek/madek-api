require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'Getting a preview resource without authentication' do
  before :example do
    @media_entry = FactoryBot.create(:media_entry_with_image_media_file,
                                      get_metadata_and_previews: false)
    @preview = @media_entry.media_file.previews.sample
  end

  shared_context :check_not_authenticated_without_public_permission do
    it 'is forbidden 401' do
      expect(response.status).to be == 401
    end
  end

  include_context :check_preview_resource_via_any,
                  :check_not_authenticated_without_public_permission
end

describe 'Getting a preview resource with authentication' do
  before :example do
    @media_entry = FactoryBot.create(
      :media_entry_with_image_media_file,
      get_metadata_and_previews: false,
      responsible_user: FactoryBot.create(:user))
    @preview = @media_entry.media_file.previews.sample
    @entity = FactoryBot.create(:user, password: 'password')
  end

  include_context :auth_preview_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    before :example do
      @media_entry.user_permissions << \
        FactoryBot.create(:media_entry_user_permission,
                           get_metadata_and_previews: false,
                           user: @entity)
      group = FactoryBot.create(:group)
      @entity.groups << group
      @media_entry.group_permissions << \
        FactoryBot.create(:media_entry_group_permission,
                           get_metadata_and_previews: false,
                           group: group)
    end
    it 'is forbidden 403' do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_responsible do
    before :example do
      @media_entry.update! responsible_user: @entity
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_permission do
    before :example do
      @media_entry.user_permissions << \
        FactoryBot.create(:media_entry_user_permission,
                           get_metadata_and_previews: true,
                           user: @entity)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_group_permission do
    before :example do
      group = FactoryBot.create(:group)
      @entity.groups << group
      @media_entry.group_permissions << \
        FactoryBot.create(:media_entry_group_permission,
                           get_metadata_and_previews: true,
                           group: group)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end
end
