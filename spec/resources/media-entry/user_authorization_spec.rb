require "spec_helper"
require Pathname(File.expand_path("..", __FILE__)).join("shared")

describe "Getting a media-entry resource without authentication" do
  before :example do
    @media_entry = FactoryBot.create(:media_entry,
                                     get_metadata_and_previews: false)
  end

  shared_context :check_not_authenticated_without_public_permission do
    it "is forbidden 401" do
      expect(response.status).to be == 401
    end
  end

  include_context :check_media_entry_resource_via_any,
                  :check_not_authenticated_without_public_permission
end

describe "Getting a media-entry resource with authentication" do
  before :example do
    @media_entry = FactoryBot.create(
      :media_entry, get_metadata_and_previews: false,
                    responsible_user: FactoryBot.create(:user),
    )
    @entity = FactoryBot.create(:user, password: "password")
  end

  include_context :auth_media_entry_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    before :example do
      @media_entry.user_permissions << FactoryBot.create(:media_entry_user_permission,
                                                         get_metadata_and_previews: false,
                                                         user: @entity)
      group = FactoryBot.create(:group)
      @entity.groups << group
      @media_entry.group_permissions << FactoryBot.create(:media_entry_group_permission,
                                                          get_metadata_and_previews: false,
                                                          group: group)
    end
    it "is forbidden 403" do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_responsible_user do
    before :example do
      @media_entry.update! responsible_user: @entity
    end

    it "is allowed 200" do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_belongs_to_responsible_delegation do
    before do
      delegation = create(:delegation)
      delegation.users << @entity
      @media_entry.update!(
        responsible_user: nil,
        responsible_delegation_id: delegation.id,
      )
    end

    it "is allowed 200" do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_belongs_to_group_belonging_to_responsible_delegation do
    before do
      delegation = create(:delegation)
      group = create(:group)
      delegation.groups << group
      group.users << @entity
      @media_entry.update!(
        responsible_user: nil,
        responsible_delegation_id: delegation.id,
      )
    end

    it "is allowed 200" do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_permission do
    before :example do
      @media_entry.user_permissions << FactoryBot.create(:media_entry_user_permission,
                                                         get_metadata_and_previews: true,
                                                         user: @entity)
    end

    it "is allowed 200" do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_group_permission do
    before :example do
      group = FactoryBot.create(:group)
      @entity.groups << group
      @media_entry.group_permissions << FactoryBot.create(:media_entry_group_permission,
                                                          get_metadata_and_previews: true,
                                                          group: group)
    end

    it "is allowed 200" do
      expect(response.status).to be == 200
    end
  end
end
