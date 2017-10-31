require 'spec_helper'

context 'removing a user from a group via DELETE' do

  before :each do
    @group = FactoryGirl.create :institutional_group
    @user = FactoryGirl.create :user, institutional_id: SecureRandom.uuid
    @group.users << @user
  end

  it 'the user does belong to the group' do
    expect(@group.users.reload.map(&:id)).to include(@user[:id])
  end


  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do


      describe 'removing a user from the group via DELETE' do

        it 'responds with 204' do
          expect(
            client.get.relation('group').get(id: @group.id) \
            .relation('user').delete(user_id: @user.id).response.status
          ).to be== 204
        end

        it 'effectively removes the user from the group' do
          client.get.relation('group').get(id: @group.id) \
            .relation('user').delete(user_id: @user.id)
          expect(@group.users.reload.map(&:id)).not_to include(@user[:id])
        end

      end

    end

  end

end
