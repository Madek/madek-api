require 'spec_helper'
require 'set'
require Pathname(File.expand_path('..', __FILE__)).join('media-entry', 'shared')


shared_examples :check_data_includes_excatly_the_keys do |keys|
  it "the data includes exactly the keys #{keys}" do
    expect(Set.new(data.keys.map(&:to_sym))).to \
      be == Set.new(keys.map(&:to_sym))
  end
end

shared_context :with_public_preview_and_metadata_permission do
  before :each do
    @media_entry.update_attributes! get_metadata_and_previews: true
  end
end

shared_context :check_success_and_data_with_public_permission do
  context 'with public preview and metadata permission' do
    include_context :with_public_preview_and_metadata_permission

    it 'succeeds 200' do
      expect(response.status).to be == 200
    end

    context 'the body' do
      let :data do
        response.body
      end

      include_examples :check_data_includes_excatly_the_keys,
                       [:created_at, :creator_id, :id,
                        :is_published, :responsible_user_id].sort
    end
  end
end

shared_context :content_type_part do
  let :content_type do
    response.headers['content-type'].split(';').first
  end
end

context 'Getting a media-entry resource without authentication' do
  before :each do
    @media_entry = FactoryGirl.create :media_entry
  end

  include_context :check_media_entry_resource_via_any,
                  :check_success_and_data_with_public_permission

  context 'with public preview and metadata permission' do
    include_context :with_public_preview_and_metadata_permission

    context 'for plain json' do
      include_context :media_entry_resource_via_plain_json
      describe 'the content-type part of the content-type header' do
        include_context :content_type_part
        it do
          expect(content_type).to be == 'application/json'
        end
      end
    end

    context 'for json-roa' do
      include_context :media_entry_resource_via_json_roa
      describe 'the content-type part of the content-type header' do
        include_context :content_type_part
        it do
          expect(content_type).to be == 'application/json-roa+json'
        end
      end

      describe 'the relation meta-data ' do
        let :relation_meta_data do
          resource.relation('meta-data')
        end
        describe 'meta_data' do
          it do
            expect(relation_meta_data).to be
          end
          it do
            expect(relation_meta_data).to be_a JSON_ROA::Client::Relation
          end
        end
      end
    end
  end
end
