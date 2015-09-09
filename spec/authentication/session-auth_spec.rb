require 'spec_helper'
require 'madek_open_session'
require 'cgi'
require 'timecop'

shared_examples :responds_with_success do
  it 'responds with success 200' do
    expect(response.status).to be == 200
  end
end

shared_examples :responds_with_not_authorized do
  it 'responds with 401 not authorized' do
    expect(response.status).to be == 401
  end
end

shared_context :valid_session_object do |to_include|
  context 'valid session object' do
    let :session_cookie do
      CGI::Cookie.new('name' => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                      'value' => MadekOpenSession.build_session_value(user))
    end

    let :client do
      json_roa_client do |conn|
        conn.headers['Cookie'] = session_cookie.to_s
      end
    end

    include_examples to_include
  end
end

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

  context 'Session authentication is enabled' do
    include_examples :valid_session_object, :responds_with_success

    context 'expired session object' do
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

      include_examples :responds_with_not_authorized
      it 'the body indicates that the session has expired' do
        expect(response.body).to match(/has expired/)
      end
    end
  end

  context 'Session authentication is disabled ' do
    before :each do
      @original_config_local = YAML.load_file(
        'config/settings.local.yml') rescue {}
      config_local = @original_config_local.merge(
        'madek_api_session_enabled' => false)
      File.open('config/settings.local.yml', 'w') do |f|
        f.write config_local.to_yaml
      end
      sleep 3
    end

    after :each do
      File.open('config/settings.local.yml', 'w') do |f|
        f.write @original_config_local.to_yaml
      end
    end

    include_examples :valid_session_object, :responds_with_not_authorized
  end
end
