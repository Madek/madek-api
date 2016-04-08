require 'spec_helper'

describe 'index' do
  include_context :json_roa_client_for_authenticated_user do
    let :meta_keys_resource do
      json_roa_client.get.relation('meta-keys').get
    end

    it 'should return 200 with only viewable by public meta-keys' do
      vocab = FactoryGirl.create(:vocabulary, enabled_for_public_view: false)
      meta_key = FactoryGirl.create(:meta_key,
                                    id: "#{vocab.id}:#{Faker::Lorem.word}",
                                    vocabulary: vocab)
      expect(meta_keys_resource.response.status).to be == 200
      expect(meta_keys_resource.data['meta-keys'].count).to be == 0
    end
  end
end
