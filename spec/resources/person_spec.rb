require "spec_helper"

context "Getting a person resource without authentication" do
  before :each do
    @person = FactoryBot.create :person
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/people/#{@person.id}")
  end

  let :json_roa_person_resource do
    JSON_ROA::Client.connect("#{api_base_url}/people/#{@person.id}")
  end

  it "responds with 200" do
    expect(json_roa_person_resource.get.response.status).to be == 200

    ["institution", "description", "first_name", "external_uris",
     "updated_at", "id", "last_name", "pseudonym", "created_at", "subtype"].each do |key|
      expect(json_roa_person_resource.get.data.keys).to include key
    end

    expect(
      json_roa_person_resource
        .get.relation("root").get.response.status
    ).to be == 200
    expect(plain_json_response.status).to be == 200
  end
end
