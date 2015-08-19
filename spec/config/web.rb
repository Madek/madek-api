require 'capybara/rspec'
require 'selenium-webdriver'
require 'json_roa/client'

def api_port
  @api_port ||= Settings.services.api.http.port
end

def api_base_url
  @api_base_url ||= "http://localhost:#{api_port}/api"
end

def json_roa_client
  @json_roa_client ||= JSON_ROA::Client.connect api_base_url,
                                                raise_error: false
end

def plain_faraday_json_client
  @plain_faraday_json_client ||= Faraday.new(
    url: api_base_url,
    headers: { accept: 'application/json' }) do |conn|
      conn.adapter Faraday.default_adapter
      conn.response :json, content_type: /\bjson$/
    end
end

def set_capybara_values
  Capybara.current_driver = :selenium
  Capybara.app_host = api_base_url
  Capybara.server_port = api_port
end

RSpec.configure do |config|
  set_capybara_values

  config.before :all do
    set_capybara_values
  end

  config.before :each do
    set_capybara_values
  end
end
