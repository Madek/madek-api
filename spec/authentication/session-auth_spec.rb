require 'spec_helper'
require 'madek_open_session'
require 'cgi'

describe "Session/Cookie Authentication" do

  before :all do
    Rails.application.secrets.secret_key_base= "secret"
  end

  let :user do
    FactoryGirl.create :user, password: 'TOPSECRET'
  end

  let :session_cookie do
    CGI::Cookie.new("name" => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                    "value" => MadekOpenSession.build_session_value(user))
  end

  let :client do
    json_roa_client do |conn|
      conn.headers["Cookie"] = session_cookie.to_s
    end
  end


  let :resource do
    client.get.relation('auth-info').get
  end

  let :response do
    resource.response
  end

  it 'responds with success 200' do
    expect(response.status).to be == 200
  end

end
