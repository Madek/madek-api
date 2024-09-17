require "spec_helper"

def client_for_preview(&block)
  JSON_ROA::Client.connect \
    "#{api_base_url}/previews/#{@preview.id}",
    raise_error: false,
      &block
end

shared_context :preview_resource_via_json_roa do
  let :resource do
    client_for_preview.get
  end
  let :response do
    resource.response
  end
end

shared_context :preview_resource_via_plain_json do
  let :response do
    plain_faraday_json_client.get("/api/previews/#{@preview.id}")
  end
end

shared_context :auth_preview_resource_via_json_roa do
  def auth_client_for_preview
    client_for_preview do |conn|
      conn.basic_auth(@entity.login, @entity.password)
    end
  end

  let :resource do
    auth_client_for_preview.get
  end
  let :response do
    resource.response
  end
end

shared_context :check_preview_resource_via_any do |ctx|
  context :via_json_roa do
    include_context :preview_resource_via_json_roa
    include_context ctx
  end

  context :via_plain_json do
    include_context :preview_resource_via_plain_json
    include_context ctx
  end
end
