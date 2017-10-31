require 'spec_helper'

context 'users' do

  before :each do
    @users = 201.times.map{FactoryGirl.create :user}
  end

  context 'non admin user' do
    include_context :json_roa_client_for_authenticated_user do
      it 'is forbidden to retrieve users' do
        expect(
          client.get.relation('users').get().response.status
        ).to be==403
      end
    end
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'get users' do

        let :users_result do
          client.get.relation('users').get()
        end

        it 'responses with 200' do
          expect(users_result.response.status).to be== 200
        end

        it 'returns some data but less than created because we paginate' do
          expect(
            users_result.data()['users'].count
          ).to be< @users.count
        end

        it 'using the roa collection we can retrieve all users' do
          set_of_created_ids = Set.new(@users.map(&:id))
          set_of_retrieved_ids = Set.new(users_result.collection().map(&:get).map{|x| x.data['id']})
          expect(set_of_created_ids - set_of_retrieved_ids).to be_empty
        end

      end
    end
  end
end


