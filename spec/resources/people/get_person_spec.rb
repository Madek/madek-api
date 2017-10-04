require 'spec_helper'
require 'hashdiff'

context 'people' do

  before :each do
    @person = FactoryGirl.create :person
    @person = @person.reload
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      context 'retriving a standard person' do
        let :get_person_result do
          client.get.relation('person').get(id: @person.id)
        end

        it 'works' do
          expect(get_person_result.response.status).to be==200
        end

        it 'lets us navigate to the person itself via the self-relation' do
          expect(get_person_result.json_roa_data['self-relation']['href']).to match /#{@person.id}/
        end

        it 'has the proper data' do
          expect(
            HashDiff.diff(
              get_person_result.data.except(:searchable,:created_at,:updated_at),
              @person.attributes.with_indifferent_access.except(:searchable,:created_at,:updated_at))
          ).to be_empty
        end
      end

      context 'a institunal person (with naughty institutional_id)' do
        before :each do
          @inst_person = FactoryGirl.create :people_instgroup ,
            institutional_id: '?this#id/needs/to/be/url&encoded'
        end
        it 'can be retrieved by the institutional_id' do
          expect(
            client.get.relation('person').get(id: @inst_person.institutional_id).response.status
          ).to be== 200
          expect(
            client.get.relation('person').get(id: @inst_person.institutional_id).data["id"]
          ).to be== @inst_person["id"]
        end
      end

    end
  end
end


