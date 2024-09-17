require "spec_helper"
require "cgi"

shared_examples :responds_with_success do
  it "responds with success 200" do
    expect(response.status).to be == 200
  end
end

shared_examples :responds_with_not_authorized do
  it "responds with 401 not authorized" do
    expect(response.status).to be == 401
  end
end

shared_context :valid_session_object do |to_include|
  context "valid session object" do
    let :session_cookie do
      CGI::Cookie.new("name" => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                      "value" => session.token)
    end

    let :client do
      json_roa_client do |conn|
        conn.headers["Cookie"] = session_cookie.to_s
      end
    end

    include_examples to_include
  end
end

describe "Session/Cookie Authentication" do
  before :all do
    Rails.application.secrets.secret_key_base = "secret"
  end

  let :user do
    FactoryBot.create :user, password: "TOPSECRET"
  end

  let :auth_system do
    AuthSystem.find_by!(id: "password")
  end

  let :session do
    UserSession.create!(
      user: user,
      auth_system: auth_system,
      meta_data: { http_user_agent: "API Test",
                   remote_addr: "127.0.0.1" },
    )
  end

  let :resource do
    client.get.relation("auth-info").get
  end

  let :response do
    resource.response
  end

  context "Session authentication is enabled" do
    include_examples :valid_session_object, :responds_with_success

    context "expired session object" do
      let :session_cookie do
        cookie = CGI::Cookie.new("name" => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                                 "value" => session.token)
        auth_system.update!(session_max_lifetime_hours: 0)
        cookie
      end

      let :client do
        json_roa_client do |conn|
          conn.headers["Cookie"] = session_cookie.to_s
        end
      end

      include_examples :responds_with_not_authorized

      it "the body indicates that the session is not valid" do
        expect(response.body).to match(/No valid session found/)
      end
    end
  end
end
