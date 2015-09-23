require 'spec_helper'

shared_context :meta_datum_text_for_random_resource_type do |_ctx|
  let :media_resource do
    user = FactoryGirl.create(:user)
    case
    when rand < 1.0 / 3
      FactoryGirl.create :media_entry, creator: user, responsible_user: user
    when rand < 1.0 / 2
      FactoryGirl.create :collection, creator: user, responsible_user: user
    else
      FactoryGirl.create :filter_set, creator: user, responsible_user: user
    end
  end

  let :meta_datum do
    case media_resource
    when MediaEntry
      FactoryGirl.create :meta_datum_text,
                         media_entry: media_resource
    when Collection
      FactoryGirl.create :meta_datum_text,
                         collection: media_resource
    when FilterSet
      FactoryGirl.create :meta_datum_text,
                         filter_set: media_resource
    end
  end
end

ROUNDS = begin
           Integer(ENV['ROUNDS'].presence)
         rescue
           25
         end

describe 'generated runs' do
  (1..ROUNDS).each do |round|
    describe "ROUND #{round}" do
      describe 'meta_datum_text_for_random_resource_type' do
        include_context :meta_datum_text_for_random_resource_type
        describe 'authenticated_json_roa_client' do
          include_context :authenticated_json_roa_client
          after :each do |example|
            if example.exception
              example.exception.message << \
                "\n  MediaResource: #{media_resource} " \
                " #{media_resource.attributes}"
              example.exception.message << "\n  Client: #{client_entity} " \
                " #{client_entity.attributes}"
            end
          end
          describe 'with random public view permission' do
            before :each do
              media_resource.update_attributes! \
                get_metadata_and_previews: (rand <= 0.5)
            end
            describe 'the meta-data resource' do
              let :resource do
                authenticated_json_roa_client.get.relation('meta-datum') \
                  .get('id' => meta_datum.id)
              end

              let :response do
                resource.response
              end

              it 'status, either 200 success or 403 forbidden, '\
                  'corresponds to the get_metadata_and_previews value' do
                expect(response.status).to be == \
                  (media_resource.get_metadata_and_previews ? 200 : 403)
              end

              it 'holds the proper text value when the response is 200' do
                if response.status == 200
                  expect(resource.data['value']).to be == meta_datum.string
                end
              end
            end
          end
        end
      end
    end
  end
end
