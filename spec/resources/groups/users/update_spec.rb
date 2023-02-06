require 'spec_helper'

describe 'updating group-users' do

  before :each do

    C = 110

    @group = FactoryBot.create :institutional_group

    @current_group_users = C.times.map do
      FactoryBot.create :user, institutional_id: SecureRandom.uuid
    end

    @group.users << @current_group_users


    @current_non_group_users = C.times.map do
      FactoryBot.create :user, institutional_id: SecureRandom.uuid
    end

    @update_users = \
      @current_group_users[0..(C/2.floor)] + @current_non_group_users[0..(C/2.floor)]

    @update_data= @update_users.map do |user|
      user.slice([:id, :institutional_id, :email].sample)
    end

  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      let :roa_response do
        client.get.relation('group').get(id: @group.id).relation('users').put do |req|
          req.body = {users: @update_data}.to_json
          req.headers['Content-Type'] = 'application/json'
        end
      end

      it 'works and sets the group users to exactly those given with the request' do
        expect(roa_response.response.status).to be== 204
        expect(
          Set.new(@group.users.reload.map(&:id))
        ).to be== Set.new(@update_users.map(&:id))

      end
    end
  end
end


