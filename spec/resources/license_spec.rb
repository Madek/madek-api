require 'spec_helper'

context 'Getting a keyword resource without authentication' do
  before :each do
    @license = FactoryGirl.create :license
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/licenses/#{@license.id}")
  end

  let :json_roa_license_resource do
    JSON_ROA::Client.connect("#{api_base_url}/licenses/#{@license.id}")
  end

  it 'responds with 200' do
    expect(json_roa_license_resource.get.response.status)
      .to be == 200
    expect(
      json_roa_license_resource
        .get.relation('root').get.response.status
    ).to be == 200
    expect(plain_json_response.status).to be == 200
  end
end


