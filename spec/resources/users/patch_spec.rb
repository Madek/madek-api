require 'spec_helper'

context 'groups' do

  before :each do
    @group = FactoryBot.create :group
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'patching/updating' do
        it 'works' do
          expect(
            client.get.relation('group').patch(id: @group.id) do |req|
              req.body = {name: "new name"}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status
          ).to be== 200
        end

        it 'works when we do no changes' do
          expect(
            client.get.relation('group').patch(id: @group.id) do |req|
              req.body = {name: @group.name}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status
          ).to be== 200
        end

        context 'patch result' do
          let :patch_result do
            client.get.relation('group').patch(id: @group.id) do |req|
              req.body = {name: "new name"}.to_json
              req.headers['Content-Type'] = 'application/json'
            end
          end
          it 'contains the update' do
            expect(patch_result.data['name']).to be== 'new name'
          end
          it 'lets us navigate to the group via the self-relation' do
            expect(patch_result.json_roa_data['self-relation']['href']).to \
              match /#{@group.id}/
          end
        end

      end


    end
  end
end
