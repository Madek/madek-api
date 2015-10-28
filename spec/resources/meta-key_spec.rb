require 'spec_helper'

context 'Getting a meta-key resource without authentication' do
  before :each do
    @meta_key = FactoryGirl.create :meta_key_text
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/meta-keys/#{@meta_key.id}")
  end

  let :json_roa_meta_key_resource do
    JSON_ROA::Client.connect("#{api_base_url}/meta-keys/#{@meta_key.id}")
  end

  it 'responds with 200' do
    expect(json_roa_meta_key_resource.get.response.status)
      .to be == 200
    expect(
      json_roa_meta_key_resource
        .get.relation('root').get.response.status
    ).to be == 200
    expect(plain_json_response.status).to be == 200
  end
end
