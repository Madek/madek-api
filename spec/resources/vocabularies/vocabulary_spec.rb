require 'spec_helper'

describe 'vocabulary' do
  include_context :json_roa_client_for_authenticated_user do
    def json_roa_vocabulary_resource(vocabulary)
      JSON_ROA::Client.connect \
        "#{api_base_url}/vocabularies/#{vocabulary}",
        raise_error: false
    end

    it 'should return 200 for an existing vocabulary' do
      vocab = FactoryGirl.create(:vocabulary,
                                 enabled_for_public_view: true)
      expect(
        json_roa_vocabulary_resource(vocab.id).get.response.status
      ).to be == 200
    end

    it 'should return 404 for non-existing vocabulary' do
      expect(
        json_roa_vocabulary_resource('bla').get.response.status
      ).to be == 404
    end
  end
end
