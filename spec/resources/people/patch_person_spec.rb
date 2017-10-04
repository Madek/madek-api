require 'spec_helper'

context 'people' do

  before :each do
    @person = FactoryGirl.create :person
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'patching/updating' do
        it 'works' do
          expect(
            client.get.relation('person').patch(id: @person.id) do |req|
              req.body = {last_name: "new name"}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status
          ).to be== 200
        end

        it 'works when we do no changes' do
          expect(
            client.get.relation('person').patch(id: @person.id) do |req|
              req.body = {last_name: @person.last_name}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status
          ).to be== 200
        end

        context 'patch result' do
          let :patch_result do
            client.get.relation('person').patch(id: @person.id) do |req|
              req.body = {last_name: "new name"}.to_json
              req.headers['Content-Type'] = 'application/json'
            end
          end
          it 'contains the update' do
            expect(patch_result.data['last_name']).to be== 'new name'
          end
          it 'lets us navigate to the person via the self-relation' do
            expect(patch_result.json_roa_data['self-relation']['href']).to \
              match /#{@person.id}/
          end
        end

      end


    end
  end
end
