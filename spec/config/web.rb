require 'json_roa/client'

def api_port
  @api_port ||= Settings.services.api.http.port
end

def api_base_url
  @api_base_url ||= "http://localhost:#{api_port}/api"
end

def json_roa_client(&block)
  JSON_ROA::Client.connect \
    api_base_url, raise_error: false, &block
end

def plain_faraday_json_client
  @plain_faraday_json_client ||= Faraday.new(
    url: api_base_url,
    headers: { accept: 'application/json' }) do |conn|
      yield(conn) if block_given?
      conn.adapter Faraday.default_adapter
      conn.response :json, content_type: /\bjson$/
    end
end
