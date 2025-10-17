require "spec_helper"
require Pathname(File.expand_path("..", __FILE__)).join("shared")

describe "generated runs" do
  (1..ROUNDS).each do |round|
    describe "ROUND #{round}" do
      describe "meta_datum_people_for_random_resource_type" do
        include_context :meta_datum_for_random_resource_type
        let(:meta_datum_people) do
          case media_resource
          when MediaEntry
            FactoryBot.create "meta_datum_people",
                              media_entry: media_resource
          when Collection
            FactoryBot.create "meta_datum_people",
                              collection: media_resource
          end
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
                authenticated_json_roa_client.get.relation("meta-datum").get("id" => meta_datum_people.id)
              end

              let(:value) { resource.data["value"] }

              let :response do
                resource.response
              end

              it "status, either 200 success or 403 forbidden, " \
                 "corresponds to the get_metadata_and_previews value" do
                expect(response.status).to be == (media_resource.get_metadata_and_previews ? 200 : 403)
              end

              context "if the response is 200" do
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

                context "MetaDatum::Person resource (new way)" do
                  let(:root) { authenticated_json_roa_client.get }

                  it "provides valid relations via meta-data-people-id" do
                    if response.status == 200
                      resource.data["value"].each do |v|
                        meta_data_person = root.relation("meta-datum-person").get("id" => v["meta-data-people-id"])

                        expect(meta_data_person.relation("meta-datum").get.response.status).to be == 200
                        expect(meta_data_person.relation("person").get.response.status).to be == 200
                      end
                    end
                  end
                end

                context "Person resource (old way - backwards compatibility)" do
                  let(:root) { authenticated_json_roa_client.get }

                  it "provides valid person relations via id field" do
                    if response.status == 200
                      resource.data["value"].each do |v|
                        person = root.relation("person").get("id" => v["id"])

                        expect(person.response.status).to be == 200
                        expect(person.data["id"]).to eq(v["id"])
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
