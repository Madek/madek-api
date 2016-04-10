require 'spec_helper'

describe 'meta-key' do
  include_context :json_roa_client_for_authenticated_user do
    def json_roa_meta_key_resource(meta_key_id)
      JSON_ROA::Client.connect \
        "#{api_base_url}/meta-keys/#{meta_key_id}",
        raise_error: false
    end

    it 'should return 200 for an existing meta_key_id' do
      vocab = FactoryGirl.create(:vocabulary,
                                 enabled_for_public_view: true)
      meta_key = FactoryGirl.create(:meta_key,
                                    id: "#{vocab.id}:#{Faker::Lorem.word}",
                                    vocabulary: vocab)
      expect(
        json_roa_meta_key_resource(meta_key.id).get.response.status
      ).to be == 200
    end

    it 'should return 422 for malformatted meta_key_id' do
      [':bla', ':', 'bla:', 'bla'].each do |meta_key_id|
        expect(
          json_roa_meta_key_resource(meta_key_id).get.response.status
        ).to be == 422
      end
    end

    it 'should return 404 for non-existing meta_key_id' do
      expect(
        json_roa_meta_key_resource('foo:bar').get.response.status
      ).to be == 404
    end
  end
end
