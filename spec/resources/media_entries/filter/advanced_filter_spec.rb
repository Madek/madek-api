require 'spec_helper'
require Pathname(File.expand_path('datalayer/spec/models/media_entry/combined_filter_shared_context.rb'))
require Pathname(File.expand_path('datalayer/spec/models/media_entry/search_in_all_meta_data_shared_context.rb'))

describe 'advanced filtering of media entries' do
  let :media_entries_relation do
    json_roa_client.get.relation('media-entries')
  end

  def get_media_entries(filter = nil)
    media_entries_relation.get(filter).data['media-entries']
  end

  context 'applying a combined filter' do
    include_context 'meta data shared context'

    it 'returns 200 with correct result' do
      20.times do
        FactoryGirl.create \
          [:media_entry_with_image_media_file,
           :media_entry_with_audio_media_file].sample,
           :fat
      end

      filter = \
        { meta_data: meta_data_1 + meta_data_2,
          media_files: media_files_1 + media_files_2,
          permissions: permissions_1 + permissions_2 }

      fetched_media_entries = \
        get_media_entries('filter_by' => filter.deep_stringify_keys.to_json)
      expect(fetched_media_entries.size).to be == 1
      expect(fetched_media_entries.first['id']).to be == media_entry.id
    end
  end

  context 'searching a string through all meta data' do
    include_context 'search in all meta data shared context'

    it 'returns 200 with correct result' do
      media_entries = [media_entry_1,
                       media_entry_2,
                       media_entry_3,
                       media_entry_4,
                       # meta_data_groups will be deprecated: media_entry_5,
                       media_entry_6]
      filter = { meta_data: [{ key: 'any', match: 'nitai' }] }
      fetched_media_entries = \
        get_media_entries('filter_by' => filter.deep_stringify_keys.to_json)

      media_entries.each do |me|
        expect(fetched_media_entries.map { |me| me['id'] }).to include me.id
      end
      expect(fetched_media_entries.count).to be == 5
    end
  end
end
