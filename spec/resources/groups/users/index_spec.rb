require 'spec_helper'

describe 'getting the index of group-users' do

  before :each do
    @group = FactoryGirl.create :institutional_group

    @group_users = 202.times.map do
      FactoryGirl.create :user, institutional_id: SecureRandom.uuid
    end

    @group.users << @group_users

    @other_users = 12.times.map do
      FactoryGirl.create :user, institutional_id: SecureRandom.uuid
    end

  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'geting the group_users ' do

        let :group_users_result do
          client.get.relation('group').get(id: @group.id).relation('users').get()
        end

        it 'works' do
          expect(group_users_result.response.status).to be== 200
        end

        it 'returns some data but less than created because we paginate' do
          expect(
            group_users_result.data()['users'].count
          ).to be< @group_users.count
        end

        describe 'the roa collection' do
          it 'contains excactly the group users' do
            added_ids = Set.new(@group_users.map(&:id))
            retrieved_ids = Set.new(group_users_result.collection() \
              .map(&:get).map{|x| x.data['id']})
            expect(added_ids).to be== retrieved_ids
          end
        end
      end
    end
  end
end
