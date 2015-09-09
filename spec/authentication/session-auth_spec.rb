require 'spec_helper'
require 'madek_open_session'
require 'cgi'
require 'timecop'

describe 'Session/Cookie Authentication' do
  before :all do
    Rails.application.secrets.secret_key_base = 'secret'
  end

  let :user do
    FactoryGirl.create :user, password: 'TOPSECRET'
  end

  let :resource do
    client.get.relation('auth-info').get
  end

  let :response do
    resource.response
  end

  context 'valid session' do
    let :session_cookie do
      CGI::Cookie.new('name' => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                      'value' => MadekOpenSession.build_session_value(user))
    end

    let :client do
      json_roa_client do |conn|
        conn.headers['Cookie'] = session_cookie.to_s
      end
    end

    it 'responds with success 200' do
      expect(response.status).to be == 200
    end
  end

  context 'expired session' do
    let :session_cookie do
      Timecop.freeze(Time.now - 7.days) do
        CGI::Cookie.new('name' => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                        'value' => MadekOpenSession.build_session_value(user))
      end
    end

    let :client do
      json_roa_client do |conn|
        conn.headers['Cookie'] = session_cookie.to_s
      end
    end

    it 'responds with 401' do
      expect(response.status).to be == 401
    end

    it 'the body indicates that the session has expired' do
      expect(response.body).to match /has expired/
    end
  end
end
