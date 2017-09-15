require 'spec_helper'

context 'groups' do

  context 'admin user' do

    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'creating' do
        describe 'a group' do

          it 'works' do
            expect( client.get.relation('groups').post do |req|
              req.body = {}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status).to be== 201
          end
        end

        describe 'an institutional group' do
          it 'works' do
            expect( client.get.relation('groups').post do |req|
              req.body = {type: "InstitutionalGroup", institutional_group_id: "12345_x"}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status).to be== 201
          end
        end
      end

      describe 'a via post created group' do
        let :created_group do
          client.get.relation('groups').post do |req|
            req.body = {type: "InstitutionalGroup", institutional_group_id: "12345/x"}.to_json
            req.headers['Content-Type'] = 'application/json'
          end
        end
        describe 'the data' do
          it 'has the proper type' do
            expect(created_group.data['type']).to be== "InstitutionalGroup"
          end
          it 'has the proper institutional_group_id' do
            expect(created_group.data['institutional_group_id']).to be== "12345/x"
          end
        end
        describe 'the json-roa-data' do
          it 'lets us navigate to the group via the self-relation' do
            expect(created_group.json_roa_data['self-relation']['href']).to \
              match /#{created_group.data['id']}/
          end
        end
      end
    end
  end
end
