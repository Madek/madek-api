require 'spec_helper'

context 'Getting a role resource without authentication' do
  before :each do
    @role = FactoryBot.create :role
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/roles/#{@role.id}")
  end

  let :json_roa_role_resource do
    JSON_ROA::Client.connect("#{api_base_url}/roles/#{@role.id}")
  end

  it 'responds with 200' do
    expect(json_roa_role_resource.get.response.status)
      .to be == 200
    expect(plain_json_response.status).to be == 200
  end

  it 'provides proper relations' do
    expect(
      json_roa_role_resource
        .get.relation('root').get.response.status
    ).to be == 200
  end
end
