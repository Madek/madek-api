require 'spec_helper'
require Pathname(File.expand_path('../../', __FILE__)).join('shared')

describe 'filtering filter_sets' do
  include_context :bunch_of_filter_sets

  let :filter_sets_relation do
    client.get.relation('filter-sets')
  end

  def get_filter_sets(filter = nil)
    filter_sets_relation.get(filter).data['filter-sets']
  end

  context 'permission params checks' do
    include_context :json_roa_client_for_authenticated_user do
      it 'returns 422 if some \'me_\' not true' do
        response = \
          filter_sets_relation.get('me_get_metadata_and_previews' => false)
          .response
        expect(response.status).to be == 422
      end
    end
  end

  context 'by public_ permissions' do
    include_context :json_roa_client_for_authenticated_user do
      it 'public_get_metadata_and_previews' do
        get_filter_sets('public_get_metadata_and_previews' => 'true')
          .each do |fs|
          filter_set = FilterSet.unscoped.find(fs['id'])
          expect(filter_set.get_metadata_and_previews).to be true
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
            FactoryGirl.create(:filter_set,
                               get_metadata_and_previews: true)
          }

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(filter_set.get_metadata_and_previews).to be true
          end
        end

        it '200 for responsible user' do
          10.times {
            FactoryGirl.create(:filter_set,
                               responsible_user: user,
                               get_metadata_and_previews: false)
          }

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(filter_set.responsible_user).to be == user
          end
        end

        it '200 for user permission' do
          10.times do
            FactoryGirl.create \
              :filter_set_user_permission,
              filter_set: FactoryGirl.create(:filter_set,
                                             get_metadata_and_previews: false),
              user: user
          end

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(filter_set.user_permissions.first.user).to be == user
          end
        end

        it '200 for group permission' do
          10.times do
            g = FactoryGirl.create(:group)
            user.groups << g
            FactoryGirl.create \
              :filter_set_group_permission,
              filter_set: FactoryGirl.create(:filter_set,
                                             get_metadata_and_previews: false),
              group: g
          end

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(user.groups)
              .to include filter_set.group_permissions.first.group
          end
        end
      end
     end


    context 'me_get_metadata_and_previews for an api_client' do
      include_context :json_roa_client_for_authenticated_api_client do
        it '200 for public permissions' do
          10.times {
            FactoryGirl.create(:filter_set,
                               get_metadata_and_previews: true)
          }

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(filter_set.get_metadata_and_previews).to be true
          end
        end

        it '200 for api_client permission' do
          10.times do
            FactoryGirl.create \
              :filter_set_api_client_permission,
              filter_set: FactoryGirl.create(:filter_set,
                                             get_metadata_and_previews: false),
              api_client: api_client
          end

          get_filter_sets('me_get_metadata_and_previews' => 'true')
            .each do |fs|
            filter_set = FilterSet.unscoped.find(fs['id'])
            expect(filter_set.api_client_permissions.first.api_client)
              .to be == api_client
          end
        end
      end
    end
  end
end
