
shared_context :json_roa_client_for_authenticated_user do |_ctx|
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

  describe 'JSON-ROA `client` for authenticated `user`' do |ctx|
    include_context ctx if ctx
  end
end
