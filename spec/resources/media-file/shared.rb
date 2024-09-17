require "spec_helper"

shared_context :media_file_resource_via_json_roa do
  let :resource do
    json_roa_client.get.relation("media-file").get("id" => @media_file.id)
  end
  let :response do
    resource.response
  end
end

shared_context :auth_media_file_resource_via_json_roa do
  let :client do
    json_roa_client do |conn|
      conn.basic_auth(@entity.login, @entity.password)
    end
  end
  let :resource do
    client.get.relation("media-file").get("id" => @media_file.id)
  end
  let :response do
    resource.response
  end
end

shared_context :auth_media_file_original_data_stream_via_json_roa do
  let :client do
    json_roa_client do |conn|
      conn.basic_auth(@entity.login, @entity.password)
    end
  end
  let :resource do
    client.get.relation("media-file").get("id" => @media_file.id).relation("data-stream").get()
  end
  let :response do
    resource.response
  end
end

shared_context :media_file_resource_via_plain_json do
  let :response do
    plain_faraday_json_client.get("/api/media-files/#{@media_file.id}")
  end
end

shared_context :check_media_file_resource_via_any do |ctx|
  context :via_json_roa do
    include_context :media_file_resource_via_json_roa
    include_context ctx
  end

  context :via_plain_json do
    include_context :media_file_resource_via_plain_json
    include_context ctx
  end
end
