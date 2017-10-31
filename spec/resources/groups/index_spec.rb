require 'spec_helper'

context 'groups' do

  before :each do
    @groups = 201.times.map{FactoryGirl.create :group}
  end

  context 'non admin user' do
    include_context :json_roa_client_for_authenticated_user do
      it 'is forbidden to retrieve groups' do
        expect(
          client.get.relation('groups').get().response.status
        ).to be==403
      end
    end
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'get groups' do

        let :groups_result do
          client.get.relation('groups').get()
        end

        it 'responses with 200' do
          expect(groups_result.response.status).to be== 200
        end

        it 'returns some data but less than created because we paginate' do
          expect(
            groups_result.data()['groups'].count
          ).to be< @groups.count
        end

        it 'using the roa collection we can retrieve all groups' do
          set_of_created_ids = Set.new(@groups.map(&:id))
          set_of_retrieved_ids = Set.new(groups_result.collection().map(&:get).map{|x| x.data['id']})
          expect(set_of_retrieved_ids.count).to be== set_of_created_ids.count
          expect(set_of_retrieved_ids).to be== set_of_created_ids
        end

      end
    end
  end
end


