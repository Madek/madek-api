require 'spec_helper'

context 'people' do

  before :each do
    @person = FactoryBot.create :person
  end

  context 'non admin user' do
    include_context :json_roa_client_for_authenticated_user do
      it 'is forbidden to delete any person' do
        expect(
          client.get.relation('person').delete(id: @person.id).response.status
        ).to be== 403
      end
    end
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      context 'deleting a standard person' do
        let :delete_person_result do
          client.get.relation('person').delete(id: @person.id)
        end

        it 'returns the expected status code 204' do
          expect(delete_person_result.response.status).to be==204
        end

        it 'effectively removes the person' do
          expect(delete_person_result.response.status).to be==204
          expect(Group.find_by(id: @person.id)).not_to be
        end

      end
    end
  end
end
