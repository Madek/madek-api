require 'spec_helper'
require "#{Rails.root}/spec/resources/collection/shared.rb"

describe 'Getting a collection resource without authentication' do
  before :example do
    @collection = FactoryBot.create(:collection,
                                     get_metadata_and_previews: false)
  end

  shared_context :check_not_authenticated_without_public_permission do
    it 'is forbidden 401' do
      expect(response.status).to be == 401
    end
  end

  include_context :check_collection_resource_via_any,
                  :check_not_authenticated_without_public_permission
end

describe 'Getting a collection resource with authentication' do
  before :example do
    @collection = FactoryBot.create(
      :collection, get_metadata_and_previews: false,
                   responsible_user: FactoryBot.create(:user))
    @entity = FactoryBot.create(:user, password: 'password')
  end

  include_context :auth_collection_resource_via_json_roa

  context :check_forbidden_without_required_permission do
    before :example do
      @collection.user_permissions << \
        FactoryBot.create(:collection_user_permission,
                           get_metadata_and_previews: false,
                           user: @entity)
      group = FactoryBot.create(:group)
      @entity.groups << group
      @collection.group_permissions << \
        FactoryBot.create(:collection_group_permission,
                           get_metadata_and_previews: false,
                           group: group)
    end
    it 'is forbidden 403' do
      expect(response.status).to be == 403
    end
  end

  context :check_allowed_if_responsible do
    before :example do
      @collection.update! responsible_user: @entity
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_belongs_to_responsible_delegation do
    before do
      delegation = create(:delegation)
      delegation.users << @entity
      @collection.update!(
        responsible_user: nil,
        responsible_delegation_id: delegation.id
      )
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_belongs_to_group_belonging_to_responsible_delegation do
    before do
      delegation = create(:delegation)
      group = create(:group)
      delegation.groups << group
      group.users << @entity
      @collection.update!(
        responsible_user: nil,
        responsible_delegation_id: delegation.id
      )
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end

  context :check_allowed_if_user_permission do
    before :example do
      @collection.user_permissions << \
        FactoryBot.create(:collection_user_permission,
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
      @collection.group_permissions << \
        FactoryBot.create(:collection_group_permission,
                           get_metadata_and_previews: true,
                           group: group)
    end

    it 'is allowed 200' do
      expect(response.status).to be == 200
    end
  end
end
