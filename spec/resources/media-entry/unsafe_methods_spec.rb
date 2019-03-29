require 'spec_helper'

describe 'Unsafe methods not possible for session auth' do
  before :all do
    Rails.application.secrets.secret_key_base = 'secret'
  end

  let :media_entry do
    FactoryGirl.create(:media_entry)
  end

  let :user do
    FactoryGirl.create(:user)
  end

  let :session_cookie do
    CGI::Cookie.new('name' => Madek::Constants::MADEK_SESSION_COOKIE_NAME,
                    'value' => MadekOpenSession.build_session_value(user))
  end

  let :session_client do
    plain_faraday_json_client do |conn|
      conn.request :url_encoded
      conn.headers['Cookie'] = session_cookie.to_s
    end
  end

  it 'put' do
    @resp = session_client.put("media-entries/#{media_entry.id}",
                               is_published: false)
  end

  it 'post' do
    @resp = session_client.post("media-entries/#{media_entry.id}",
                                is_published: false)
  end

  it 'delete' do
    @resp = session_client.delete("media-entries/#{media_entry.id}")
  end

  after :example do
    expect(@resp.status).to eq 405
  end
end

