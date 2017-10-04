require 'spec_helper'

context 'people' do

  context 'admin user' do

    include_context :json_roa_client_for_authenticated_admin_user do

      describe 'creating' do
        describe 'a person' do

          it 'works' do
            expect( client.get.relation('people').post do |req|
              req.body = { last_name: 'test',
                           subtype: 'Person'}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status).to be== 201
          end
        end

        describe 'an institutional person' do
          it 'works' do
            expect( client.get.relation('people').post do |req|
              req.body = { first_name: nil,
                           last_name: 'Bachelor',
                           pseudonym: 'BA.alle',
                           searchable: 'Bachelor BA.alle',
                           institutional_id: '162645.alle',
                           subtype: 'PeopleInstitutionalGroup'}.to_json
              req.headers['Content-Type'] = 'application/json'
            end.response.status).to be== 201
          end
        end
      end

      describe 'a via post created person' do
        let :created_person do
          client.get.relation('people').post do |req|
            req.body = {subtype: "PeopleInstitutionalGroup",
                        institutional_id: "12345/x",
                        last_name: 'test'}.to_json
            req.headers['Content-Type'] = 'application/json'
          end
        end
        describe 'the data' do
          it 'has the proper subtype' do
            expect(created_person.data['subtype']).to be== "PeopleInstitutionalGroup"
          end
          it 'has the proper institutional_id' do
            expect(created_person.data['institutional_id']).to be== "12345/x"
          end
        end
        describe 'the json-roa-data' do
          it 'lets us navigate to the person via the self-relation' do
            binding.pry
            expect(created_person.json_roa_data['self-relation']['href']).to \
              match /#{created_person.data['id']}/
          end
        end
      end
    end
  end
end
