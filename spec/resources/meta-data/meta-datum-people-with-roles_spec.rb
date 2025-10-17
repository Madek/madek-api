require "spec_helper"
require Pathname(File.expand_path("..", __FILE__)).join("shared")

ROUNDS = 3.freeze

describe "generated runs" do
  (1..ROUNDS).each do |round|
    describe "ROUND #{round}" do
      describe "meta_datum_people_with_roles_for_media_entry" do
        include_context :meta_datum_for_media_entry
        let(:meta_datum_people_with_roles) do
          FactoryBot.create "meta_datum_people_with_roles",
                            media_entry: media_resource
        end

        describe "authenticated_json_roa_client" do
          include_context :authenticated_json_roa_client
          after :each do |example|
            if example.exception
              example.exception.message << "\n  MediaResource: #{media_resource} " \
                                           " #{media_resource.attributes}"
              example.exception.message << "\n  Client: #{client_entity} " \
              " #{client_entity.attributes}"
            end
          end
          describe "with random public view permission" do
            before :each do
              media_resource.update! \
                get_metadata_and_previews: (rand <= 0.5)
            end
            describe "the meta-data resource" do
              let :resource do
                authenticated_json_roa_client.get.relation("meta-datum").get("id" => meta_datum_people_with_roles.id)
              end

              let :response do
                resource.response
              end

              it "status, either 200 success or 403 forbidden, " \
                 "corresponds to the get_metadata_and_previews value" do
                expect(response.status).to be == (media_resource.get_metadata_and_previews ? 200 : 403)
              end

              context "if the response is 200" do
                let(:value) { resource.data["value"] }

                it "it holds the proper uuid array value with both old and new fields" do
                  if response.status == 200
                    value.each do |v|
                      # New way: meta-data-people-id should reference MetaDatum::Person record
                      expect(v).to have_key("meta-data-people-id")
                      expect(MetaDatum::Person.find_by(meta_datum_id: resource.data["id"],
                                                       id: v["meta-data-people-id"])).to be

                      # Old way: id should reference person_id for backwards compatibility
                      expect(v).to have_key("id")
                      meta_datum_person = MetaDatum::Person.find_by(meta_datum_id: resource.data["id"],
                                                                    id: v["meta-data-people-id"])
                      expect(meta_datum_person.person_id).to eq(v["id"])
                    end
                  end
                end

                context "MetaDatum::Person with Role resource" do
                  let(:root) { authenticated_json_roa_client.get }

                  it "provides valid relations" do
                    if response.status == 200
                      resource.data["value"].each do |v|
                        meta_data_person = root.relation("meta-datum-person").get("id" => v["meta-data-people-id"])

                        expect(meta_data_person.relation("meta-datum").get.response.status).to be == 200
                        expect(meta_data_person.relation("person").get.response.status).to be == 200
                        unless meta_data_person.data["role_id"].nil?
                          expect(meta_data_person.relation("role").get.response.status).to be == 200
                        end
                      end
                    end
                  end

                  context "role is assigned" do
                    it "has role relation" do
                      if response.status == 200
                        resource.data["value"].each do |v|
                          meta_data_person = root.relation("meta-datum-person").get("id" => v["meta-data-people-id"])

                          unless meta_data_person.data["role_id"].nil?
                            expect(meta_data_person.json_roa_data["relations"]).to have_key "role"
                          end
                        end
                      end
                    end
                  end

                  context "role is not assigned" do
                    it "has no role relation" do
                      if response.status == 200
                        resource.data["value"].each do |v|
                          meta_data_person = root.relation("meta-datum-person").get("id" => v["meta-data-people-id"])

                          if meta_data_person.data["role_id"].nil?
                            expect(meta_data_person.json_roa_data["relations"]).not_to have_key "role"
                          end
                        end
                      end
                    end
                  end
                end

                it "it provides valid collection and relations" do
                  if response.status == 200
                    # Collection should contain both person links (old way) and meta-datum-person links (new way)
                    person_ids = value.map { |v| v["id"] }
                    meta_data_people_ids = value.map { |v| v["meta-data-people-id"] }
                    all_ids = person_ids + meta_data_people_ids

                    resource.collection.each do |c_entry|
                      expect(c_entry.get.response.status).to be == 200
                      expect(all_ids).to include c_entry.get.data["id"]
                    end

                    expect(resource.relation("meta-key").get.response.status).to be == 200
                    expect(resource.relation("media-entry").get.response.status).to be == 200
                  end
                end
              end
            end
          end
        end
      end
    end
  end
end
