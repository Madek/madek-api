require "spec_helper"

context "Getting a keyword resource without authentication" do
  before :each do
    @keyword = FactoryBot.create(:keyword, external_uris: ["http://example.com"])
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/keywords/#{@keyword.id}")
  end

  let :json_roa_keyword_resource do
    JSON_ROA::Client.connect("#{api_base_url}/keywords/#{@keyword.id}")
  end

  it "responds with 200" do
    expect(json_roa_keyword_resource.get.response.status).to be == 200
    expect(
      json_roa_keyword_resource
        .get.relation("meta-key").get.response.status
    ).to be == 200
    expect(
      json_roa_keyword_resource
        .get.relation("root").get.response.status
    ).to be == 200
    expect(plain_json_response.status).to be == 200
  end

  it "has the proper data" do
    keyword = json_roa_keyword_resource.get.data
    expect(
      keyword.except(:created_at, :updated_at)
    ).to eq(
      @keyword.attributes.with_indifferent_access
        .except(:searchable, :position, :creator_id, :created_at, :updated_at)
        .merge(external_uri: keyword[:external_uris].first)
    )
  end
end
