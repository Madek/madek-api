require 'spec_helper'

context 'Getting a keyword resource without authentication' do
  before :each do
    @person = FactoryGirl.create :person
  end

  let :plain_json_response do
    plain_faraday_json_client.get("/api/people/#{@person.id}")
  end

  let :json_roa_person_resource do
    JSON_ROA::Client.connect("#{api_base_url}/people/#{@person.id}")
  end

  it 'responds with 200' do
    expect(json_roa_person_resource.get.response.status)
      .to be == 200
    expect(
      json_roa_person_resource
        .get.relation('root').get.response.status
    ).to be == 200
    expect(plain_json_response.status).to be == 200
  end
end

