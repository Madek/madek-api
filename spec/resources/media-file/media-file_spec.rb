require 'spec_helper'

describe 'MediaFile Resource' do
  include_context :authenticated_json_roa_client

  describe 'requesting a non existing media-resource' do
    let :resource do
      authenticated_json_roa_client.get.relation('media-file') \
        .get('id' => SecureRandom.uuid)
    end
    let :response do
      resource.response
    end

    it 'responds with 404 not found' do
      expect(response.status).to be == 404
    end
  end
end
