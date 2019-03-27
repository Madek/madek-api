require 'spec_helper'

describe 'vocabulary' do
  include_context :json_roa_client_for_authenticated_user do
    ###############################################################################
    # Just so that there is some other arbitrary data besides the actual test data.
    # No exlicit expectations are done with them.
    before :example do
      10.times do
        FactoryGirl.create(:vocabulary,
                           enabled_for_public_view: [true, false].sample)
      end

      Vocabulary.take(5).shuffle.each do |vocabulary|
        Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                      view: [true, false].sample,
                                                      vocabulary: vocabulary)

        group = FactoryGirl.create :group
        group.users << user
        Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                       view: [true, false].sample,
                                                       vocabulary: vocabulary)
      end
    end
    ###############################################################################

    def json_roa_vocabulary_resource(vocabulary_id, is_authenticated_user = false, params = {})
      query_params = URI.encode_www_form(params)
      query_params = '?' + query_params unless query_params.empty?
      JSON_ROA::Client.connect(
        "#{api_base_url}/vocabularies/#{vocabulary_id}#{query_params}",
        raise_error: false) do |conn|
          if is_authenticated_user
            conn.basic_auth(entity.login, entity.password)
          end
        end
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

    it 'does not return admin_comment property' do
      vocabulary = FactoryGirl.create(:vocabulary,
                                      enabled_for_public_view: true)

      expect(json_roa_vocabulary_resource(vocabulary.id).get.response.body)
        .not_to have_key 'admin_comment'
    end

    describe 'accessibility' do
      it 'returns public vocabulary' do
        vocabulary = FactoryGirl.create(:vocabulary,
                                        enabled_for_public_view: true)

        data = json_roa_vocabulary_resource(vocabulary.id).get.response.body

        expect(data).to have_key 'id'
        expect(data['id']).to eq vocabulary.id
      end

      it 'does not return non-public vocabulary' do
        vocabulary = FactoryGirl.create(:vocabulary,
                                        enabled_for_public_view: false)

        data = json_roa_vocabulary_resource(vocabulary.id).get.response.body

        expect(data).not_to have_key 'id'
        expect(data['message']).to eq 'Vocabulary could not be found!'
      end

      context 'when user is authenticated' do
        context 'when view permission is true' do
          it 'returns vocabulary through the user permissions' do
            vocabulary = FactoryGirl.create(:vocabulary,
                                            enabled_for_public_view: false)
            Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                          view: true,
                                                          vocabulary: vocabulary)

            data = json_roa_vocabulary_resource(vocabulary.id, true).get.response.body

            expect(data).to have_key 'id'
            expect(data['id']).to eq vocabulary.id
          end

          it 'returns vocabulary through the group permissions' do
            vocabulary = FactoryGirl.create(:vocabulary,
                                            enabled_for_public_view: false)
            group = FactoryGirl.create :group
            group.users << user
            Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                           view: true,
                                                           vocabulary: vocabulary)

            data = json_roa_vocabulary_resource(vocabulary.id, true).get.response.body

            expect(data).to have_key 'id'
            expect(data['id']).to eq vocabulary.id
          end
        end

        context 'when view permission is false' do
          it 'does not return vocabulary through the user permissions' do
            vocabulary = FactoryGirl.create(:vocabulary,
                                            enabled_for_public_view: false)
            Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                          view: false,
                                                          vocabulary: vocabulary)

            data = json_roa_vocabulary_resource(vocabulary.id, true).get.response.body

            expect(data).not_to have_key 'id'
            expect(data['message']).to eq 'Vocabulary could not be found!'
          end

          it 'does not return vocabulary through the group permissions' do
            vocabulary = FactoryGirl.create(:vocabulary,
                                            enabled_for_public_view: false)
            group = FactoryGirl.create :group
            group.users << user
            Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                           view: false,
                                                           vocabulary: vocabulary)

            data = json_roa_vocabulary_resource(vocabulary.id, true).get.response.body

            expect(data).not_to have_key 'id'
            expect(data['message']).to eq 'Vocabulary could not be found!'
          end
        end
      end
    end

    describe 'multilingual labels' do
      let(:vocabulary) do
        FactoryGirl.create(
          :vocabulary,
          labels: {
            de: 'label de',
            en: 'label en'
          })
      end

      specify 'result contains correct labels' do
        expect(json_roa_vocabulary_resource(vocabulary.id).get.data['labels'])
          .to eq({ 'de' => 'label de', 'en' => 'label en'})
      end

      context 'when locale param is not present' do
        it 'returns a label for a default locale' do
          expect(
            json_roa_vocabulary_resource(vocabulary.id).get.data['label']
          ).to eq 'label de'
        end
      end

      context 'when locale param is present' do
        it 'returns a correct label for "en" locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :en }).get.data['label']
          ).to eq 'label en'
        end

        it 'returns a correct label for "de" locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :de }).get.data['label']
          ).to eq 'label de'
        end
      end

      context 'when locale param is not available' do
        it 'returns a label for a default locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :pl }).get.data['label']
          ).to eq 'label de'
        end
      end
    end

    describe 'multilingual descriptions' do
      let(:vocabulary) do
        FactoryGirl.create(
          :vocabulary,
          descriptions: {
            de: 'description de',
            en: 'description en'
          })
      end

      specify 'result contains correct descriptions' do
        expect(
          json_roa_vocabulary_resource(vocabulary.id).get.data['descriptions']
        )
          .to eq({ 'de' => 'description de', 'en' => 'description en' })
      end

      context 'when locale param is not present' do
        it 'returns a description for a default locale' do
          expect(
            json_roa_vocabulary_resource(vocabulary.id).get.data['description']
          ).to eq 'description de'
        end
      end

      context 'when locale param is present' do
        it 'returns a correct description for "en" locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :en }).get.data['description']
          ).to eq 'description en'
        end

        it 'returns a correct description for "de" locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :de }).get.data['description']
          ).to eq 'description de'
        end
      end

      context 'when locale param is not available' do
        it 'returns a description for a default locale' do
          expect(
            json_roa_vocabulary_resource(
              vocabulary.id,
              false,
              { lang: :pl }).get.data['description']
          ).to eq 'description de'
        end
      end
    end
  end
end
