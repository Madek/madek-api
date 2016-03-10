require 'spec_helper'
require Pathname(File.expand_path('../../', __FILE__)).join('shared')

describe 'filtering collections' do
  include_context :bunch_of_collections

  let :collections_relation do
    client.get.relation('collections')
  end

  def get_collections(filter = nil)
    collections_relation.get(filter).data['collections']
  end

  context 'permission params checks' do
    include_context :json_roa_client_for_authenticated_user do
      it 'returns 422 if some \'me_\' not true' do
        response = \
          collections_relation.get('me_get_metadata_and_previews' => false)
          .response
        expect(response.status).to be == 422
      end
    end
  end

  context 'by public_ permissions' do
    include_context :json_roa_client_for_authenticated_user do
      it 'public_get_metadata_and_previews' do
        get_collections('public_get_metadata_and_previews' => 'true')
          .each do |c|
          collection = Collection.unscoped.find(c['id'])
          expect(collection.get_metadata_and_previews).to be true
        end
      end
    end
  end

  context 'by me_ permissons' do
    let :media_entries_relation do
      client.get.relation('media-entries')
    end

    context 'me_get_metadata_and_previews for a user' do
      include_context :json_roa_client_for_authenticated_user do
        it '200 for public permissions' do
          10.times {
            FactoryGirl.create(:collection,
                               get_metadata_and_previews: true)
          }

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(collection.get_metadata_and_previews).to be true
          end
        end

        it '200 for responsible user' do
          10.times {
            FactoryGirl.create(:collection,
                               responsible_user: user,
                               get_metadata_and_previews: false)
          }

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(collection.responsible_user).to be == user
          end
        end

        it '200 for user permission' do
          10.times do
            FactoryGirl.create \
              :collection_user_permission,
              collection: FactoryGirl.create(:collection,
                                             get_metadata_and_previews: false),
              user: user
          end

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(collection.user_permissions.first.user).to be == user
          end
        end

        it '200 for group permission' do
          10.times do
            g = FactoryGirl.create(:group)
            user.groups << g
            FactoryGirl.create \
              :collection_group_permission,
              collection: FactoryGirl.create(:collection,
                                             get_metadata_and_previews: false),
              group: g
          end

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(user.groups)
              .to include collection.group_permissions.first.group
          end
        end
      end
     end


    context 'me_get_metadata_and_previews for an api_client' do
      include_context :json_roa_client_for_authenticated_api_client do
        it '200 for public permissions' do
          10.times {
            FactoryGirl.create(:collection,
                               get_metadata_and_previews: true)
          }

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(collection.get_metadata_and_previews).to be true
          end
        end

        it '200 for api_client permission' do
          10.times do
            FactoryGirl.create \
              :collection_api_client_permission,
              collection: FactoryGirl.create(:collection,
                                             get_metadata_and_previews: false),
              api_client: api_client
          end

          get_collections('me_get_metadata_and_previews' => 'true')
            .each do |c|
            collection = Collection.unscoped.find(c['id'])
            expect(collection.api_client_permissions.first.api_client)
              .to be == api_client
          end
        end
      end
    end
  end
end
