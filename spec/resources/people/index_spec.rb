require 'spec_helper'

context 'people' do

  before :each do
    @people = 201.times.map{FactoryBot.create :person}
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'get people' do

        let :people_result do
          client.get.relation('people').get()
        end

        it 'responses with 200' do
          expect(people_result.response.status).to be== 200
        end

        it 'returns some data but less than created because we paginate' do
          expect(
            people_result.data()['people'].count
          ).to be< @people.count
        end

        it 'using the roa collection we can retrieve all people' do
          set_of_created_ids = Set.new(@people.map(&:id))
          set_of_retrieved_ids = Set.new(people_result.collection().map(&:get).map{|x| x.data['id']})
          expect(set_of_retrieved_ids.count - User.count).to be== set_of_created_ids.count
          expect(set_of_retrieved_ids - User.all.map(&:person).map(&:id) ).to be== set_of_created_ids
        end

      end
    end
  end
end


