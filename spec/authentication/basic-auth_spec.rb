require 'spec_helper'

shared_context :user_entity do |ctx|
  context 'for Database User' do
    before :each do
      @entity = FactoryGirl.create :user, password: 'TOPSECRET'
    end
    let :entity_type do
      'User'
    end
    include_context ctx if ctx
  end
end

shared_context :api_client_entity do |ctx|
  context 'for Database ApiClient' do
    before :each do
      @entity = FactoryGirl.create :api_client, password: 'TOPSECRET'
    end
    let :entity_type do
      'ApiClient'
    end
    include_context ctx if ctx
  end
end

shared_context :test_bad_password_basic_auth do
  context 'with proper username but bad password' do
    let :client do
      json_roa_client do |conn|
        conn.basic_auth(@entity.login, 'BOGUS')
      end
    end
    let :resource do
      client.get.relation('auth-info').get
    end
    let :response do
      resource.response
    end
    it 'responds with 401' do
      expect(response.status).to be == 401
    end
  end
end

shared_context :test_proper_basic_auth do
  context 'with proper username and password' do
    let :client do
      json_roa_client do |conn|
        conn.basic_auth(@entity.login, @entity.password)
      end
    end

    let :resource do
      client.get.relation('auth-info').get
    end

    let :response do
      resource.response
    end

    it 'responds with success 200' do
      binding.pry
      expect(response.status).to be == 200
    end

    describe 'the response body' do
      let :body do
        response.body
      end

      describe 'the login property' do
        let :login do
          body['login']
        end

        it 'should be equal to the entities login' do
          expect(login).to be == @entity.login
        end
      end

      describe 'the authentication-method property' do
        let :authentication_method do
          body['authentication-method']
        end
        it do
          expect(authentication_method).to be == 'Basic Authentication'
        end
      end

      describe 'the type property' do
        let :type_property do
          body['type']
        end
        it do
          expect(type_property).to be == entity_type
        end
      end
    end
  end
end

describe '/auth-info resource' do
  context 'without any authentication' do
    context 'via json roa' do
      let :resource do
        json_roa_client.get.relation('auth-info').get
      end
      let :response do
        resource.response
      end

      it 'responds with not authorized 401' do
        expect(response.status).to be == 401
      end
    end
  end

  context 'Basic Authentication' do
    include_context :user_entity, :test_proper_basic_auth
    include_context :api_client_entity, :test_proper_basic_auth
    include_context :user_entity, :test_bad_password_basic_auth
    include_context :api_client_entity, :test_bad_password_basic_auth
  end
end
