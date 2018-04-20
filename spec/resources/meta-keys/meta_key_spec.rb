require 'spec_helper'

describe 'meta-key' do
  include_context :json_roa_client_for_authenticated_user do
    def json_roa_meta_key_resource(meta_key_id, params = {})
      query_params = URI.encode_www_form(params)
      query_params = '?' + query_params unless query_params.empty?
      JSON_ROA::Client.connect \
        "#{api_base_url}/meta-keys/#{meta_key_id}#{query_params}",
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

    describe 'multilanguage labels' do
      let(:vocabulary) { FactoryGirl.create :vocabulary }
      let(:meta_key) do
        FactoryGirl.create :meta_key,
                           id: "#{vocabulary.id}:#{Faker::Lorem.word}",
                           vocabulary: vocabulary
      end
      before do
        Settings.madek_available_locales.each do |locale|
          meta_key.labels[locale] = "label #{locale}"
        end
        meta_key.save!
      end

      context 'when locale param is not present' do
        it 'returns a label for a default locale' do
          expect(
            json_roa_meta_key_resource(meta_key.id).get.data['label']
          ).to eq 'label de'
        end
      end

      context 'when locale param is present' do
        it 'returns a correct label for "en" locale' do
          expect(
            json_roa_meta_key_resource(meta_key.id, { lang: :en }).get.data['label']
          ).to eq 'label en'
        end

        it 'returns a correct label for "de" locale' do
          expect(
            json_roa_meta_key_resource(meta_key.id, { lang: :de }).get.data['label']
          ).to eq 'label de'
        end
      end

      context 'when locale param is not available' do
        it 'returns a label for a default locale' do
          expect(
            json_roa_meta_key_resource(meta_key.id, { lang: :pl }).get.data['label']
          ).to eq 'label de'
        end
      end
    end
  end
end
