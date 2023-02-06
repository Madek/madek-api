require 'spec_helper'

describe 'roles' do

  before :each do
    @roles = 201.times.map do |i|
      FactoryBot.create :role, labels: { de: "Role #{i}" }
    end
  end

  include_context :authenticated_json_roa_client do
    describe 'get roles' do

      let :roles_result do
        authenticated_json_roa_client.get.relation('roles').get
      end

      it 'responses with 200' do
        expect(roles_result.response.status).to be == 200
      end

      it 'returns some data but less than created because we paginate' do
        expect(
          roles_result.data['roles'].count
        ).to be < @roles.count
      end

      it 'retrieves all roles using the roa collection' do
        set_of_created_ids = Set.new(@roles.map(&:id))
        set_of_retrieved_ids = Set.new(roles_result.collection.map(&:get).map { |x| x.data['id'] })
        expect(set_of_retrieved_ids.count - set_of_created_ids.count).to be_zero
      end
    end
  end
end
