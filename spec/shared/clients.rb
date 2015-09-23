shared_context :json_roa_client_for_authenticated_user do |ctx|
  let :user do
    FactoryGirl.create :user, password: 'TOPSECRET'
  end

  let :entity do
    user
  end

  let :client do
    json_roa_client do |conn|
      conn.basic_auth(entity.login, entity.password)
    end
  end

  describe 'JSON-ROA `client` for authenticated `user`' do
    include_context ctx if ctx
  end
end

shared_context :authenticated_json_roa_client do |_ctx|
  if rand < 0.5
    let :user do
      FactoryGirl.create :user, password: 'TOPSECRET'
    end
    let :api_client do
      nil
    end
  else
    let :user do
      nil
    end
    let :api_client do
      FactoryGirl.create :api_client, password: 'TOPSECRET'
    end
  end

  let :client_entity do
    user || api_client
  end

  let :authenticated_json_roa_client do
    json_roa_client do |conn|
      conn.basic_auth(client_entity.login, client_entity.password)
    end
  end
end
